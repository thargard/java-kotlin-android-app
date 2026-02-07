import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AnnouncementsComponent } from './components/announcements/announcements.component';
import { UserPortfolioComponent } from './components/user-portfolio/user-portfolio.component';
import { ProductsComponent } from './components/products/products.component';
import { ProductDetailComponent } from './components/product-detail/product-detail.component';
import { ProductCreateComponent } from './components/product-create/product-create.component';
import { OrderDetailComponent } from './components/order-detail/order-detail.component';
import { ChatComponent } from './components/chat/chat.component';
import { MessagesComponent } from './components/messages/messages.component';
import { CartComponent } from './components/cart/cart.component';

export const routes: Routes = [
  // Главная страница
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent },
  // Заказы
  { path: 'orders/:id', component: OrderDetailComponent },
  // Чат
  { path: 'chat/:threadId', component: ChatComponent },
  // Сообщения (список диалогов)
  { path: 'messages', component: MessagesComponent },
  // Объявления
  { path: 'announcements', component: AnnouncementsComponent },
  // Корзина
  { path: 'cart', component: CartComponent },
  // Готовые товары
  { path: 'products', component: ProductsComponent },
  { path: 'products/create', component: ProductCreateComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  // Портфолио пользователей
  { path: 'users/:id', component: UserPortfolioComponent },
  // Backward-compatible route
  { path: 'home', redirectTo: '', pathMatch: 'full' },
];
