/** 共用分頁回應，T代表每筆資料的型別 */
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}