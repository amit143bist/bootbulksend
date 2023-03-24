import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CacheDataLogListComponent } from './cachedatalog-list/cachedatalog-list.component';
import { CreateCacheDataLogComponent } from './create-cachedatalog/create-cachedatalog.component';
import { UpdateCacheDataLogComponent } from './update-cachedatalog/update-cachedatalog.component';
import { CacheDataLogDetailsComponent } from './cachedatalog-details/cachedatalog-details.component';

const routes: Routes = [
  { path: '', redirectTo: 'cachedatalogs', pathMatch: 'full' },
  { path: 'cachedatalogs', component: CacheDataLogListComponent },
  { path: 'add', component: CreateCacheDataLogComponent },
  { path: 'update/:cacheId', component: UpdateCacheDataLogComponent },
  { path: 'details/:cacheId', component: CacheDataLogDetailsComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
