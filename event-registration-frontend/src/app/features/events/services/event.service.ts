import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../../../core/models/page-response';
import { EventResponse, EventAvailabilityResponse } from '../models/event-response';


@Injectable({
    providedIn: 'root',
})
export class EventService {
    private readonly http = inject(HttpClient);

    /** 分頁查詢已發布活動，頁碼從 0 開始。 */
    getEvents(
        page: number = 0,
        size: number = 10,
        keyword: string = ''
    ): Observable<PageResponse<EventResponse>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);

        //有關鍵字才加入搜尋條件，先去除前後空白。
        const normalizedKeyword = keyword.trim();

        if (normalizedKeyword) {
            params = params.set('keyword', normalizedKeyword);
        }


        return this.http.get<PageResponse<EventResponse>>(
            '/api/events', { params, }
        );
    }

    /**依活動 ID 查詢已發布活動，草稿或不存在的活動會回傳404 */
    getEventById(id: number): Observable<EventResponse> {
        return this.http.get<EventResponse>(
            `/api/events/${id}`
        );
    }

    /** 查詢活動目前的有效報名人數與剩餘名額。 */
    getAvailability(id: number): Observable<EventAvailabilityResponse> {
        return this.http.get<EventAvailabilityResponse>(
            `/api/events/${id}/availability`
        );
    }
}