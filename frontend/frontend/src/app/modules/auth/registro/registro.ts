
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.html',
  styleUrls: ['./registro.scss'],
  standalone: false
})
export class RegistroComponent implements OnInit {

  form!: FormGroup;
  loading   = false;
  error     = '';
  exitoso   = '';
  showPass  = false;
  showPass2 = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.form = this.fb.group({
      nombre:           ['', [Validators.required, Validators.minLength(2)]],
      apellido:         ['', [Validators.required, Validators.minLength(2)]],
      email:            ['', [Validators.required, Validators.email]],
      telefono:         ['', [Validators.required, Validators.pattern(/^[0-9]{7,15}$/)]],
      password:         ['', [Validators.required, Validators.minLength(8),
                              Validators.pattern(/^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).*$/)]],
      confirmarPassword:['', Validators.required],
      rol:              ['ASESOR', Validators.required],
      terminos:         [false, Validators.requiredTrue]
    }, { validators: this.passwordsIguales });
  }

  passwordsIguales(group: FormGroup) {
    const pass = group.get('password')?.value;
    const confirm = group.get('confirmarPassword')?.value;
    return pass === confirm ? null : { noCoinciden: true };
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error   = '';

    const request = {
      nombre:           this.form.value.nombre,
      apellido:         this.form.value.apellido,
      email:            this.form.value.email,
      telefono:         this.form.value.telefono,
      password:         this.form.value.password,
      confirmarPassword: this.form.value.confirmarPassword
    };

    this.authService.registro(request).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.exitoso = '¡Cuenta creada exitosamente! Redirigiendo...';
          setTimeout(() => this.router.navigate(['/dashboard']), 2000);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.error   = err.error?.message || 'Error al crear la cuenta';
        this.cdr.detectChanges();
      }
    });
  }

  irALogin(): void {
    this.router.navigate(['/auth/login']);
  }

  get nombre()            { return this.form.get('nombre')!; }
  get apellido()          { return this.form.get('apellido')!; }
  get email()             { return this.form.get('email')!; }
  get telefono()          { return this.form.get('telefono')!; }
  get password()          { return this.form.get('password')!; }
  get confirmarPassword() { return this.form.get('confirmarPassword')!; }
  get terminos()          { return this.form.get('terminos')!; }

  get passwordNoCoincide(): boolean {
    return this.form.hasError('noCoinciden') &&
           this.form.get('confirmarPassword')!.touched;
  }
}