import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService
  ) {}

  onSubmit() {
    if (!this.username || !this.password) {
      this.errorMessage = this.translate.instant('login.errorRequired');
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        this.isLoading = false;
        // Перенаправление на главную страницу или дашборд
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('Login error', error);
        this.errorMessage =
          error.message || this.translate.instant('login.errorFailed');
        this.isLoading = false;
      },
    });
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
