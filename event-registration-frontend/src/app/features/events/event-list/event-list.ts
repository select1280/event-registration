import { Component, inject, OnInit, signal } from '@angular/core';
import { EventResponse } from '../models/event-response';
import { EventService } from '../services/event.service';
import { finalize } from 'rxjs';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-event-list',
  imports: [ReactiveFormsModule],
  templateUrl: './event-list.html',
  styleUrl: './event-list.scss',
})
export class EventList implements OnInit {
  private readonly eventService = inject(EventService);

  //管理活動資料與畫面載入狀態。
  readonly events = signal<EventResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  //頁碼從0開始，分頁數由後端計算。
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize = 10;

  //輸入框內容與實際套用的搜尋條件分開管理。
  readonly keywordControl = new FormControl('', { nonNullable: true });
  private activeKeyword = '';

  /** 元件初始化時，載入第一頁活動。 */
  ngOnInit(): void {
    this.loadEvents();
  }

  /** 查詢活動;成功更新列表，失敗顯示提示。 */
  loadEvents(page: number = this.currentPage()): void {
    //避免同時送出多筆查詢，或查詢負數頁碼。
    if (this.loading() || page < 0) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.eventService.getEvents(page, this.pageSize, this.activeKeyword)
      .pipe(
        //無論成功或失敗，都要結束載入狀態。
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          this.events.set(response.content);
          this.currentPage.set(response.page);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => {
          this.errorMessage.set('活動載入失敗，請稍後再試');
        }
      });
  }

  /** 套用搜尋條件，並從第一頁開始查詢 */
  onSearch(): void {
    if (this.loading()) {
      return;
    }

    this.activeKeyword = this.keywordControl.value.trim();
    this.loadEvents(0);
  }
}
