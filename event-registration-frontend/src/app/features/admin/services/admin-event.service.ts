import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { PageResponse } from "../../../core/models/page-response";
import { EventResponse } from "../../events/models/event-response";
import { Observable, switchMap } from "rxjs";
import { AuthService } from "../../../core/services/auth.service";
import { EventRequest } from "../models/event-request";

@Injectable({
    providedIn: 'root',
})
export class AdminEventService {

    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

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

    /** 管理員建立活動草稿，送出前先取得 CSRF token。 */
    createEvent(request: EventRequest): Observable<EventResponse> {
        return this.authService.getCsrfToken().pipe(
            switchMap((csrf) =>
                this.http.post<EventResponse>('/api/events', request, {
                    headers: {
                        [csrf.headerName]: csrf.token,
                    },
                })
            )
        );
    }
}