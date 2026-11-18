/** 對應後端註冊請求，不接受由前端指定會員角色。 */
export interface RegisterRequest {
    email: string;
    password: string;
    displayName: string;
}