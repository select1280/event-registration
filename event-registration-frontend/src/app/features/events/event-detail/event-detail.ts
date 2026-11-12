import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EventResponse, EventAvailabilityResponse } from '../models/event-response';
import { EventService } from '../services/event.service';
import { DatePipe } from '@angular/common';
import { RegistrationService } from '../../registrations/services/registration.service';


@Component({
  selector: 'app-event-detail',
  imports: [
    DatePipe,
    RouterLink,
  ],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.scss',
})
export class EventDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly eventService = inject(EventService);
  private readonly registrationService = inject(RegistrationService);

  // 尚未取得活動資料時，用 null 表示。
  readonly event = signal<EventResponse | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  //儲存目前查到的名額資訊，尚未取得時為null。
  readonly availability = signal<EventAvailabilityResponse | null>(null);

  //報名狀態與活動載入狀態分開，避免報名時隱藏整個活動畫面。
  readonly registering = signal(false);
  readonly registrationMessage = signal('');

  /** 讀取網址中的活動 ID，驗證後查詢詳細資料。 */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.errorMessage.set('活動 ID 格式不正確。');
      return;
    }

    this.loading.set(true);

    //同時查詢活動與名額，兩者都成功才顯示完整資訊。
    forkJoin({
      detail: this.eventService.getEventById(id),
      availability: this.eventService.getAvailability(id),
    })
      .pipe(
        // 不論查詢成功或失敗，都結束載入狀態。
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          this.event.set(response.detail);
          this.availability.set(response.availability);
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage.set(
            error.status === 404
              ? '找不到此活動，或活動尚未發布。'
              : '活動載入失敗，請稍後再試。'
          );
        },
      });
  }

  /** 以目前登入者身分報名，並阻擋重複送出。 */
  register(): void {
    const detail = this.event();

    if (!detail || this.registering()) {
      return;
    }

    this.registering.set(true);
    this.registrationMessage.set('');

    this.registrationService.register(detail.id)
      .pipe(
        // 無論成功或失敗，都恢復報名按鈕狀態。
        finalize(() => this.registering.set(false))
      )
      .subscribe({
        next: () => {
          this.registrationMessage.set('報名成功！');

          //報名成功後，重新查詢名額資訊。
          this.refreshAvailability(detail.id);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401) {
            this.registrationMessage.set('登入已失效，請重新登入。');
            return;
          }

          // 只顯示符合後端業務錯誤格式的訊息。
          if (
            error.status === 400 &&
            error.error?.code === 'BUSINESS_RULE_VIOLATION' &&
            typeof error.error?.message === 'string'
          ) {
            this.registrationMessage.set(error.error.message);
            return;
          }

          if (error.status === 403) {
            this.registrationMessage.set('請求驗證失敗，請重新整理後再試。');
            return;
          }

          this.registrationMessage.set('報名失敗，請稍後再試。');
        },
      });
  }

  /** 重新查詢名額;查詢失敗不影響已成功地報名結果。 */
  private refreshAvailability(eventId: number): void {
    //清除舊數值，避免繼續顯示報名前的名額。
    this.availability.set(null);

    this.eventService.getAvailability(eventId)
      .subscribe({
        next: (response) => {
          this.availability.set(response);
        },
        error: () => {
          this.registrationMessage.set(
            '報名成功!但名額資訊更新失敗，請重新整理頁面。'
          );
        },
      });
  }
}