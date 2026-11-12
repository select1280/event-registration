import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { EventResponse } from '../models/event-response';
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

  /** 讀取網址中的活動 ID，驗證後查詢詳細資料。 */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.errorMessage.set('活動 ID 格式不正確。');
      return;
    }

    this.loading.set(true);

    this.eventService.getEventById(id)
      .pipe(
        // 不論查詢成功或失敗，都結束載入狀態。
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          this.event.set(response);
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