import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { PageResponse } from "../../../core/models/page-response";
import { EventResponse } from "../../events/models/event-response";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root',
})
export class AdminEventService {
    private readonly http = inject(HttpClient);

    /** 管理員分頁查詢全部活動，包含草稿與已取消活動 */
    getEvents(
        page: number = 0,
        size: number = 10
    ): Observable<PageResponse<EventResponse>> {
        const params = new HttpParams()
            .set('page', page)
            .set('size', size);

        return this.http.get<PageResponse<EventResponse>>(
            '/api/admin/events',
            { params }
        );
    }
}