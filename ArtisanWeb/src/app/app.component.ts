import { Component, OnInit } from '@angular/core';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  standalone: true,
  imports: [CommonModule, TranslateModule, RouterModule],
})
export class AppComponent implements OnInit {
  title = 'ArtisanWeb';
  currentLang = 'en';
  isAuthenticated = false;

  constructor(
    private translate: TranslateService,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    // Можно получить язык из localStorage или браузера
    const browserLang = this.translate.getBrowserLang();
    const savedLang = localStorage.getItem('language');
    this.currentLang =
      savedLang || (browserLang?.match(/en|ru/) ? browserLang : 'en');
    this.translate.use(this.currentLang);

    // Подписываемся на состояние авторизации
    this.authService.currentUser$.subscribe((user) => {
      this.isAuthenticated = !!user;
    });
  }

  switchLanguage(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('language', lang);
  }

  navigateToHome() {
    this.router.navigate(['/']);
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
  }

  navigateToRegister() {
    this.router.navigate(['/register']);
  }

  navigateToAnnouncements() {
    this.router.navigate(['/announcements']);
  }

  navigateToProducts() {
    this.router.navigate(['/products']);
  }

  navigateToCart() {
    this.router.navigate(['/cart']);
  }

  navigateToProfile() {
    this.router.navigate(['/profile']);
  }

  navigateToMessages() {
    this.router.navigate(['/messages']);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
