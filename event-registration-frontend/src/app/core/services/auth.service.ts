import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { CsrfToken } from '../models/csrf-token';
import { MemberResponse } from '../models/member-response';
import { Router } from '@angular/router';

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    // 透過 Angular 的依賴注入取得 HttpClient。
    private readonly http = inject(HttpClient);

    private readonly router = inject(Router);

    /** 取得 CSRF token，供登入等修改資料的請求使用。 */
    getCsrfToken(): Observable<CsrfToken> {
        return this.http.get<CsrfToken>('/api/auth/csrf');
    }

    /** 依目前的登入 Session，取得登入者資料。 */
    getCurrentMember(): Observable<MemberResponse> {
        return this.http.get<MemberResponse>('/api/auth/me');
    }

    /** 先取得 CSRF token，再以表單格式送出登入資料。 */
    login(email: string, password: string): Observable<void> {
        const body = new HttpParams()
            .set('email', email)
            .set('password', password);

        return this.getCsrfToken().pipe(
            switchMap((csrf) =>
                this.http.post<void>('/api/auth/login', body, {
                    headers: {
                        [csrf.headerName]: csrf.token,
                    },
                })
            )
        );
    }
}