import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrestamoDetalle } from './prestamo-detalle';

describe('PrestamoDetalle', () => {
  let component: PrestamoDetalle;
  let fixture: ComponentFixture<PrestamoDetalle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrestamoDetalle],
    }).compileComponents();

    fixture = TestBed.createComponent(PrestamoDetalle);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
