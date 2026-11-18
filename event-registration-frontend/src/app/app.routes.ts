import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { EventList } from './features/events/event-list/event-list';
import { EventDetail } from './features/events/event-detail/event-detail';
import { MyRegistrations } from './features/registrations/my-registrations/my-registrations';
import { authGuard } from './core/guards/auth.guard';
import { Register } from './features/auth/register/register';
import { adminGuard } from './core/guards/admin.guard';
import { AdminEventList } from './features/admin/admin-event-list/admin-event-list';

export const routes: Routes = [
    //首頁自動導向登入頁，只有完整路徑才套用。
    {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
    },
    //Login 顯示登入元件。
    {
        path: 'login',
        component: Login,
    },
    // 活動列表頁，後續加入搜尋與分頁
    {
        path: 'events',
        component: EventList,
        //登入後才能查看活動跟個人報名
        canActivate: [authGuard],
    },
    //透過活動ID，顯示指定活動的詳細資料。
    {
        path: 'events/:id',
        component: EventDetail,
        canActivate: [authGuard],
    },
    //查看個人報名
    {
        path: 'my-registrations',
        component: MyRegistrations,
        canActivate: [authGuard],
    },
    {
        path: 'register',
        component: Register
    },
    {
        path: 'admin/events',
        component: AdminEventList,
        canActivate: [adminGuard],
    }
];
