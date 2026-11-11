/** 對應後端活動資料，日期經 JSON 傳輸後為字串 */
export interface EventResponse {
    id: number;
    title: string;
    description: string;
    location: string;
    capacity: number;
    status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
    registrationDeadline: string;
    startAt: string;
    endAt: string;
    createdAt: string;
}