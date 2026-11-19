import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminEventCreate } from './admin-event-create';

describe('AdminEventCreate', () => {
  let component: AdminEventCreate;
  let fixture: ComponentFixture<AdminEventCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminEventCreate],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminEventCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
