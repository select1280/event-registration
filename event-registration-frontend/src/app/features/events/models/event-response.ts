/** 對應後端活動資料，日期經 JSON 傳輸後為字串 */
export interface EventResponse {
    id: number;
    title: string;
    description: string;
    location: string;
    capacity: number;
    status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
    registrationDeadline: string;
    startsAt: string;
    endsAt: string;
    createdAt: string;
}

/** 活動名額的查詢快照，不代表替使用者保留名額。 */
export interface EventAvailabilityResponse {
    eventId: number;
    capacity: number;
    registeredCount: number;
    remainingCapacity: number;
}