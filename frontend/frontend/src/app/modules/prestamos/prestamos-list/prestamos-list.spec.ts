import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrestamosList } from './prestamos-list';

describe('PrestamosList', () => {
  let component: PrestamosList;
  let fixture: ComponentFixture<PrestamosList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrestamosList],
    }).compileComponents();

    fixture = TestBed.createComponent(PrestamosList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
