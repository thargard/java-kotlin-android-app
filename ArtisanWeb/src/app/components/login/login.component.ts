import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { environment } from '../../../environments/environment';

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
        this.router.navigate(['/home']);
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

  onGoogleLogin(response: { credential: string }) {
    const idToken = response.credential;
    this.isLoading = true;
    this.authService.googleLogin(idToken).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/home']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Google login failed';
      },
    });
  }

  ngAfterViewInit() {
    // @ts-ignore
    if (window.google && google.accounts && google.accounts.id) {
      // @ts-ignore
      google.accounts.id.initialize({
        client_id: environment.clientId,
        callback: (response: any) => this.onGoogleLogin(response),
      });
      // @ts-ignore
      google.accounts.id.renderButton(
        document.getElementById('googleSignInButton'),
        { theme: 'outline', size: 'large' }
      );
    }
  }
}
