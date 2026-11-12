import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { RegistrationResponse } from '../models/registration-response';
import { PageResponse } from '../../../core/models/page-response';

@Injectable({
    providedIn: 'root',
})
export class RegistrationService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    /** 使用目前登入者的身分報名活動，送出前先取得 CSRF token。 */
    register(eventId: number): Observable<RegistrationResponse> {
        return this.authService.getCsrfToken().pipe(
            switchMap((csrf) =>
                this.http.post<RegistrationResponse>(
                    `/api/events/${eventId}/registrations`,
                    null,
                    {
                        headers: {
                            [csrf.headerName]: csrf.token,
                        },
                    }
                )
            )
        );
    }

    /** 分頁查詢目前登入者的報名，包含有效與已取消的紀錄 */
    getMyRegistrations(
        page: number = 0,
        size: number = 10
    ): Observable<PageResponse<RegistrationResponse>> {
        const params = new HttpParams()
            .set('page', page)
            .set('size', size);

        return this.http.get<PageResponse<RegistrationResponse>>(
            '/api/me/registrations',
            { params }
        );
    }
}