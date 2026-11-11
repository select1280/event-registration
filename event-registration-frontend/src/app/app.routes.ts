import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { EventList } from './features/events/event-list/event-list';

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
    }
];
