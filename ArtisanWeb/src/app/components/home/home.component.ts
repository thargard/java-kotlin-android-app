import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { User } from '../../core/models/auth.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit, OnDestroy {
  login: string | null = null;
  role: string | null = null;
  private sub: Subscription | null = null;

  users: User[] = [];
  filteredUsers: User[] = [];
  loadingUsers = false;
  usersError: string | null = null;

  search = '';

  constructor(
    private authService: AuthService,
    private auth: AuthService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.sub = this.authService.currentUser$.subscribe((user) => {
      if (typeof user === 'string') {
        try {
          user = JSON.parse(user);
        } catch (e) {
          user = null;
        }
      }
      if (user) {
        this.login = user.login || null;
        this.role = user.role || null;
      } else {
        this.login = null;
        this.role = null;
      }
    });

    this.loadUsers();
  }

  loadUsers(): void {
    this.loadingUsers = true;
    this.usersError = null;
    this.auth.getAllUsers().subscribe({
      next: (users) => {
        this.users = users ?? [];
        this.applyFilter();
        this.loadingUsers = false;
      },
      error: (err) => {
        this.users = [];
        this.filteredUsers = [];
        this.loadingUsers = false;
        this.usersError =
          err?.message || this.translate.instant('home.usersError');
      },
    });
  }

  applyFilter(): void {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      this.filteredUsers = [...this.users];
      return;
    }
    this.filteredUsers = this.users.filter((u) => {
      const fullName = (u.fullName || '').toLowerCase();
      const login = (u.login || '').toLowerCase();
      return fullName.includes(term) || login.includes(term);
    });
  }

  clearSearch(): void {
    this.search = '';
    this.applyFilter();
  }

  trackByUserId(_: number, user: User): number {
    return user.id;
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
