import { Component, inject, OnInit, signal } from '@angular/core';
import { AdminEventService } from '../services/admin-event.service';
import { EventResponse } from '../../events/models/event-response';
import { finalize } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-event-list',
  imports: [
    RouterLink,
  ],
  templateUrl: './admin-event-list.html',
  styleUrl: './admin-event-list.scss',
})
export class AdminEventList implements OnInit {
  private readonly adminEventService = inject(AdminEventService);

  //管理活動資料、查詢狀態與分頁資訊。
  readonly events = signal<EventResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize = 10;

  /** 進入管理頁時載入第一頁活動。 */
  ngOnInit(): void {
    this.loadEvents();
  }

  /** 查詢全部狀態的活動，成功後更新列表與頁碼。 */
  loadEvents(page: number = this.currentPage()): void {
    if (this.loading() || page < 0) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.adminEventService.getEvents(page, this.pageSize)
      .pipe(
        //成功或失敗都結束載入狀態。
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
          this.errorMessage.set('活動管理資料載入失敗，請稍後再試。');
        },
      });
  }
}
