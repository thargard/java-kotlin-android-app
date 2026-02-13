import { Component, OnInit, OnDestroy } from '@angular/core';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { MessageSocketService } from './core/services/message-socket.service';
import { MessageBadgeService } from './core/services/message-badge.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  standalone: true,
  imports: [CommonModule, TranslateModule, RouterModule],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'ArtisanWeb';
  currentLang = 'en';
  isAuthenticated = false;
  unreadTotal = 0;
  private socketSub?: Subscription;
  private badgeSub?: Subscription;

  constructor(
    private translate: TranslateService,
    private router: Router,
    private authService: AuthService,
    private messageSocket: MessageSocketService,
    private messageBadge: MessageBadgeService,
  ) {}

  ngOnInit(): void {
    const browserLang = this.translate.getBrowserLang();
    const savedLang = localStorage.getItem('language');
    this.currentLang =
      savedLang || (browserLang?.match(/en|ru/) ? browserLang : 'en');
    this.translate.use(this.currentLang);

    this.authService.currentUser$.subscribe((user) => {
      this.isAuthenticated = !!user;
      if (user) {
        this.messageSocket.connect(user.id);
        this.messageBadge.refresh();
      } else {
        if (!this.authService.getToken()) {
          this.messageSocket.disconnect();
          this.messageBadge.setTotal(0);
        }
      }
    });

    this.badgeSub = this.messageBadge.total$.subscribe((count) => {
      this.unreadTotal = count;
    });

    this.socketSub = this.messageSocket.message$.subscribe((msg) => {
      const me = this.authService.getCurrentUser()?.id;
      if (msg.receiverId && (me == null || msg.receiverId === me)) {
        this.messageBadge.increment(1);
      }
    });

    const token = this.authService.getToken();
    if (token) {
      this.messageSocket.connect();
      this.messageBadge.refresh();
    }
  }

  ngOnDestroy(): void {
    this.socketSub?.unsubscribe();
    this.badgeSub?.unsubscribe();
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
