/**
 * 對應後端登入 /api/auth/csrf 的回應。
 * 登入等 POST 請求須將 token 放入指定的 HTTP Header。
 */
export interface CsrfToken {
    headerName: string;
    parameterName: string;
    token: string;
}