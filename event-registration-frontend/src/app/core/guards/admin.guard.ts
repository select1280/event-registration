import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";
import { catchError, map, of, throwError } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

/** 進入管理頁前，確認目前登入者是否具有管理者角色。 */
export const adminGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.getCurrentMember().pipe(
        map((member) => {
            //管理員可進入，一般會員會回活動列表。
            return member.role === 'ADMIN' ? true : router.createUrlTree(['/events']);
        }),
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
                //未登入或 Session 已失效，導回登入頁。
                return of(router.createUrlTree(['/login']));
            }

            //保留連線或伺服器錯誤，避免誤判登入狀態。
            return throwError(() => error);
        })
    );
};