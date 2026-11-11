import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize, switchMap } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  //使用 signal 管理畫面狀態，更新時會反映到模板。
  readonly submitting = signal(false);
  readonly message = signal('');

  // 集中管理登入欄位、輸入值與驗證狀態。
  readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  // 驗證表單後送出登入，並顯示執行結果。
  onSubmit(): void {
    //避免請求尚未完成時重複送出
    if (this.submitting()) {
      return;
    }

    this.message.set('');
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) {
      return;
    }

    const { email, password } = this.loginForm.getRawValue();
    this.submitting.set(true);

    this.authService.login(email, password)
      .pipe(
        // 登入成功後，再利用 Session 查詢目前登入者。
        switchMap(() => this.authService.getCurrentMember()),
        // 等登入與會員查詢全部結束，才恢復按鈕。
        finalize(() => this.submitting.set(false))
      )
      .subscribe({
        next: () => {
          this.loginForm.controls.password.reset();

          //登入與會員查詢都成功後，切換到活動列表。
          void this.router.navigate(['/events']);
        },
        error: (error: HttpErrorResponse) => {
          this.message.set(
            error.status === 401
              ? '登入驗證失敗，請確認帳密或重新登入。'
              : '登入流程發生錯誤，請稍後再試。'
          );
        },
      });
  }
}
