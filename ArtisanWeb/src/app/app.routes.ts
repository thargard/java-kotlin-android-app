import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AnnouncementsComponent } from './components/announcements/announcements.component';
import { UserPortfolioComponent } from './components/user-portfolio/user-portfolio.component';

export const routes: Routes = [
  // Главная страница
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent },
   // Объявления и портфолио пользователей
  { path: 'announcements', component: AnnouncementsComponent },
  { path: 'users/:id', component: UserPortfolioComponent },
  // Backward-compatible route
  { path: 'home', redirectTo: '', pathMatch: 'full' },
];
