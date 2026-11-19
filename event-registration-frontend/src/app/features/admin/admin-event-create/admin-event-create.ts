import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminEventService } from '../services/admin-event.service';
import { EventRequest } from '../models/event-request';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-event-create',
  imports: [
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './admin-event-create.html',
  styleUrl: './admin-event-create.scss',
})
export class AdminEventCreate {

  private readonly adminEventService = inject(AdminEventService);

  //儲存期間阻擋重複送出
  readonly submitting = signal(false);

  //顯示跨欄位驗證與後續儲存結果。
  readonly message = signal('');

  //對應活動請求欄位，日期暫存為本地時間字串。
  readonly eventForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.maxLength(120),
      ],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
      ],
    }),
    location: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.maxLength(255),
      ],
    }),
    capacity: new FormControl<number | null>(null, {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.min(1),
        Validators.max(2147483647),
        Validators.pattern(/^[0-9]+$/),
      ],
    }),
    registrationDeadline: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
      ],
    }),
    startsAt: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
      ],
    }),
    endsAt: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
      ],
    }),
  });

  /** 先驗證欄位，後續加入日期檢查與API呼叫。 */
  onSubmit(): void {

    if (this.submitting()) {
      return;
    }

    this.message.set('');
    this.eventForm.markAllAsTouched();

    if (this.eventForm.invalid) {
      return;
    }

    const values = this.eventForm.getRawValue();

    // datetime-local 沒有時區，依瀏覽器本地時區解讀。
    const deadline = new Date(values.registrationDeadline);
    const startsAt = new Date(values.startsAt);
    const endsAt = new Date(values.endsAt);

    //避免無效日期進入後續的 ISO 轉換。
    if (
      Number.isNaN(deadline.getTime()) ||
      Number.isNaN(startsAt.getTime()) ||
      Number.isNaN(endsAt.getTime())
    ) {
      this.message.set('請填寫有效的日期與時間');
      return;
    }

    if (deadline.getTime() > startsAt.getTime()) {
      this.message.set('報名截止時間不可晚於活動開始時間。');
      return;
    }

    if (endsAt.getTime() <= startsAt.getTime()) {
      this.message.set('活動結束時間必須晚於開始時間。');
      return;
    }

    //表單驗證不會自動縮小 TypeScript 型別，須明確排除 null。
    if (values.capacity === null) {
      return;
    }

    //將本地日期轉成帶有 Z 的 UTC 字串，對應後端 Instant。
    const request: EventRequest = {
      title: values.title.trim(),
      description: values.description.trim(),
      location: values.location.trim(),
      capacity: values.capacity,
      registrationDeadline: deadline.toISOString(),
      startsAt: startsAt.toISOString(),
      endsAt: endsAt.toISOString(),
    };

    this.submitting.set(true);

    this.adminEventService.createEvent(request)
      .pipe(
        //成功或失敗都恢復按鈕狀態
        finalize(() => this.submitting.set(false))
      )
      .subscribe({
        next: (event) => {
          this.message.set(`草稿建立成功，活動 ID:${event.id}。`);
          this.eventForm.reset();
        },
        error: (error: HttpErrorResponse) => {
          //顯示後端的業務規則或輸入驗證提示。
          if (
            error.status === 400 &&
            typeof error.error?.message === 'string'
          ) {
            this.message.set(error.error.message);
            return;
          }

          this.message.set('草稿儲存失敗，請確認登入權限或稍後再試');
        },
      });
  }
}
