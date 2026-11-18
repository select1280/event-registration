import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {

  private readonly authService = inject(AuthService);

  //管理送出狀態與註冊結果提示。
  readonly submitting = signal(false);
  readonly message = signal('');

  //對應後端註冊欄位，前端先檢查必填、格式與長度。
  readonly registerForm = new FormGroup({
    displayName: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.maxLength(100),
      ],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.email,
        Validators.maxLength(254),
      ],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(12),
        Validators.maxLength(128),
      ],
    }),
  });

  /** 驗證表單後建立會員，成功後引導使用者前往登入。 */
  onSubmit(): void {
    if (this.submitting()) {
      return;
    }

    this.message.set('');
    this.registerForm.markAllAsTouched();

    if (this.registerForm.invalid) {
      return;
    }

    const request = this.registerForm.getRawValue();
    this.submitting.set(true);

    this.authService.register(request)
      .pipe(
        //成功或失敗都恢復按鈕狀態。
        finalize(() => this.submitting.set(false))
      )
      .subscribe({
        next: () => {
          this.message.set('註冊成功!請點下方連結前往登入。');
          this.registerForm.reset();
        },
        error: (error: HttpErrorResponse) => {
          //顯示後端業務提示
          if (
            error.status === 400 &&
            error.error?.code === 'BUSINESS_RULE_VIOLATION' &&
            typeof error.error?.message === 'string'
          ) {
            this.message.set(error.error.message);
            return;
          }
          this.message.set('註冊失敗，請確認輸入的資料或稍後再試。');
        },
      });
  }
}
