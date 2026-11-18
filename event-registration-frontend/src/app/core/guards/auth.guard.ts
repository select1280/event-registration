import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";
import { catchError, map, of, throwError } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.getCurrentMember().pipe(
        //成功取得登入者，允許進入頁面。
        map(() => true),
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
                //回傳登入頁的UrlTree，由Router執行轉向。
                return of(router.createUrlTree(['/login']));
            }

            // 連線或伺服器錯誤保留原錯誤，避免誤判為未登入。
            return throwError(() => error);
        })
    );
};