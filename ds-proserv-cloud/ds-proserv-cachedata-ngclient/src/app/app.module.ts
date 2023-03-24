import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { CreateCacheDataLogComponent } from './create-cachedatalog/create-cachedatalog.component';
import { CacheDataLogDetailsComponent } from './cachedatalog-details/cachedatalog-details.component';
import { CacheDataLogListComponent } from './cachedatalog-list/cachedatalog-list.component';
import { UpdateCacheDataLogComponent } from './update-cachedatalog/update-cachedatalog.component';

import { FilterPipe } from './pipes/filter.pipe';
import { HighlightDirective } from './directives/highlight.pipe';
import { Ng2SmartTableModule } from 'ng2-smart-table';

@NgModule({
  declarations: [
    AppComponent,
    CreateCacheDataLogComponent,
    CacheDataLogDetailsComponent,
    CacheDataLogListComponent,
    UpdateCacheDataLogComponent,
    HighlightDirective,
    FilterPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    Ng2SmartTableModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
