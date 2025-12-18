import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit, OnDestroy {
  login: string | null = null;
  role: string | null = null;
  private sub: Subscription | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.sub = this.authService.currentUser$.subscribe((user) => {
      if (typeof user === 'string') {
        try {
          user = JSON.parse(user);
        } catch (e) {
          user = null;
        }
      }
      console.log('HomeComponent user -> ', user);
      console.log('user.login:', user?.login, 'user.role:', user?.role);
      if (user) {
        this.login = user.login || null;
        console.log('HomeComponent login -> ', this.login);
        this.role = user.role || null;
        console.log('HomeComponent role -> ', this.role);
      } else {
        this.login = null;
        this.role = null;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
