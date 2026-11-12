import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { RegistrationResponse } from '../models/registration-response';
import { RegistrationService } from '../services/registration.service';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-my-registrations',
  imports: [
    DatePipe,
    RouterLink
  ],
  templateUrl: './my-registrations.html',
  styleUrl: './my-registrations.scss',
})
export class MyRegistrations implements OnInit {
  private readonly registrationService = inject(RegistrationService);

  // 管理報名資料、載入狀態與分頁資訊。
  readonly registrations = signal<RegistrationResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize = 10;

  //紀錄正在取消的報名 ID，null 表示目前沒有取消請求。
  readonly cancellingId = signal<number | null>(null);
  readonly actionMessage = signal('');

  /** 進入頁面時，查詢目前登入者的報名。 */
  ngOnInit(): void {
    this.loadRegistrations();
  }

  /** 查詢指定頁面，成功後更新紀錄與分頁資訊。 */
  loadRegistrations(page: number = this.currentPage()): void {
    if (this.loading() || this.cancellingId() !== null || page < 0) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.registrationService.getMyRegistrations(page, this.pageSize)
      .pipe(
        // 成功或失敗都結束載入狀態。
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          this.registrations.set(response.content);
          this.currentPage.set(response.page);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => {
          this.errorMessage.set('報名紀錄載入失敗，請稍後再試。');
        },
      });
  }

  /** 取消指定報名，成功後以後端回應更新畫面。 */
  cancelRegistration(registration: RegistrationResponse): void {
    if (
      this.loading() ||
      this.cancellingId() !== null ||
      registration.status !== 'REGISTERED'
    ) {
      return;
    }

    this.cancellingId.set(registration.id);
    this.actionMessage.set('');

    //API 要活動 ID ， 不是報名紀錄 ID。
    this.registrationService.cancel(registration.eventId)
      .pipe(
        finalize(() => this.cancellingId.set(null))
      )
      .subscribe({
        next: (updated) => {
          //保留列表位置與分頁，只更新成功取消的那筆紀錄。
          this.registrations.update((items) =>
            items.map((item) => item.id === updated.id ? updated : item)
          );
          this.actionMessage.set('取消報名成功');
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401) {
            this.actionMessage.set('登入已失效，請重新登入');
            return;
          }

          //顯示後端明確的錯誤，例如「此報名已取消」。
          if (
            error.status === 400 &&
            error.error?.code === 'BUSINESS_RULE_VIOLATION' &&
            typeof error.error?.message === 'string'
          ) {
            this.actionMessage.set(error.error.message);
            return;
          }

          this.actionMessage.set('取消報名失敗，請重新載入紀錄後再試。');
        },
      });
  }
}