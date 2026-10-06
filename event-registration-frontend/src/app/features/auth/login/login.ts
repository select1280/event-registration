import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly authService = inject(AuthService);

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
        //成功或失敗都恢復按鈕狀態
        finalize(() => this.submitting.set(false))
      )
      .subscribe({
        next: () => {
          this.message.set('登入成功!');
          this.loginForm.controls.password.reset();
        },
        error: (error: HttpErrorResponse) => {
          this.message.set(
            error.status === 401
              ? 'Email 或密碼錯誤。'
              : '登入失敗，請稍後再試。'
          )
        }
      })
  }
}
