/** 對應後端報名紀錄，供報名結果與個人報名列表使用 */
export interface RegistrationResponse {
    id: number;
    eventId: number;
    eventTitle: string;
    status: 'REGISTERED' | 'CANCELLED';
    registeredAt: string;
    cancelledAt: string | null;
}