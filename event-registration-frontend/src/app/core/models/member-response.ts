/** 對應後端 MemberResponse，不包含密碼。 */
export interface MemberResponse {
  id: number;
  email: string;
  displayName: string;
  role: 'MEMBER' | 'ADMIN';
  createdAt: string;
}