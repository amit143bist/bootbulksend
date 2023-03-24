import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CacheDataLogListComponent } from './cachedatalog-list.component';

describe('CachedatalogListComponent', () => {
  let component: CacheDataLogListComponent;
  let fixture: ComponentFixture<CacheDataLogListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CacheDataLogListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CacheDataLogListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
