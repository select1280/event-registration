/** 新增或修改活動的請求，狀態與建立時間由後端管理。 */
export interface EventRequest {
    title: string;
    description: string;
    location: string;
    capacity: number;
    registrationDeadline: string;
    startsAt: string;
    endsAt: string;
}