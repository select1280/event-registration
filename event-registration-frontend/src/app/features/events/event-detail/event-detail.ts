import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EventResponse, EventAvailabilityResponse } from '../models/event-response';
import { EventService } from '../services/event.service';
import { DatePipe } from '@angular/common';


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

  // 尚未取得活動資料時，用 null 表示。
  readonly event = signal<EventResponse | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  //儲存目前查到的名額資訊，尚未取得時為null。
  readonly availability = signal<EventAvailabilityResponse | null>(null);

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
}