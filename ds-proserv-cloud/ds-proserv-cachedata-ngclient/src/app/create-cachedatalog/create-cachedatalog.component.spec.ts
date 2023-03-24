import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCachedatalogComponent } from './create-cachedatalog.component';

describe('CreateCachedatalogComponent', () => {
  let component: CreateCachedatalogComponent;
  let fixture: ComponentFixture<CreateCachedatalogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateCachedatalogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCachedatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
