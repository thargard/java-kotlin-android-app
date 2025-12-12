import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage = '';
  isLoading = false;
  passwordsMatch = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
    private formBuilder: FormBuilder
  ) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['CUSTOMER', Validators.required],
    });
  }

  checkPasswordsMatch() {
    const password = this.registerForm.get('password')?.value;
    const confirmPassword = this.registerForm.get('confirmPassword')?.value;
    this.passwordsMatch = password === confirmPassword;
  }

  onSubmit() {
    if (!this.registerForm.valid || !this.passwordsMatch) {
      this.errorMessage = this.translate.instant('register.errorInvalidForm');
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const formValue = this.registerForm.value;
    const registrationData = {
      login: formValue.username,
      email: formValue.email,
      password: formValue.password,
      fullName: formValue.fullName,
      role: formValue.role,
    };

    this.authService.register(registrationData).subscribe({
      next: (response) => {
        console.log('Registration successful', response);
        this.isLoading = false;
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Registration error', error);
        this.errorMessage =
          error.message || this.translate.instant('register.errorFailed');
        this.isLoading = false;
      },
    });
  }

  onCancel() {
    this.router.navigate(['/login']);
  }
}
