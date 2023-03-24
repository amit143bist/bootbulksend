import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CachedatalogDetailsComponent } from './cachedatalog-details.component';

describe('CachedatalogDetailsComponent', () => {
  let component: CachedatalogDetailsComponent;
  let fixture: ComponentFixture<CachedatalogDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CachedatalogDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CachedatalogDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
