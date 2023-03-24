import { Observable } from "rxjs";
import { CacheDataLogService } from "../cachedatalog.service";
import { CacheDataLog } from "../cachedatalog";
import { Component, OnInit } from "@angular/core";
import { Router } from '@angular/router';
import { LocalDataSource } from 'ng2-smart-table';

@Component({
  selector: 'app-cachedatalog-list',
  templateUrl: './cachedatalog-list.component.html',
  styleUrls: ['./cachedatalog-list.component.css']
})
export class CacheDataLogListComponent implements OnInit {

  cacheDataLogs: Observable<CacheDataLog[]>;
  
  constructor(private cacheDataLogService: CacheDataLogService,
    private router: Router) { }

  ngOnInit() {

    this.reloadData();
  }

  reloadData() {

    this.cacheDataLogService.getCacheDataLogList().subscribe((cacheLogInformation: any) => {
      this.cacheDataLogs = cacheLogInformation.cacheLogDefinitions;
    });
  }

  deleteCacheDataLog(cacheId: string) {
    this.cacheDataLogService.deleteCacheDataLogById(cacheId)
      .subscribe(
        data => {
          console.log(data);
          this.reloadData();
        },
        error => console.log(error));
  }

  updateCacheDataLog(cacheId: string) {
    
    this.router.navigate(['update', cacheId]);
  }

  cacheDataLogDetails(cacheId: string){
    this.router.navigate(['details', cacheId]);
  }

  settings = {
    delete: {
      confirmDelete: true,
      deleteButtonContent: 'Delete data',
      saveButtonContent: 'Save',
      cancelButtonContent: 'Cancel'
    },
    add: {
      confirmCreate: true,
    },
    edit: {
      confirmSave: true,
    },
    columns: {
      cacheId: {
        title: 'CacheId',
        editable: false
      },
      cacheKey: {
        title: 'CacheKey',
      },
      cacheValue: {
        title: 'CacheValue',
      },
      cacheReference: {
        title: 'CacheReference',
      },
    },
  };

  onDeleteConfirm(event) {
    console.log("Delete Event In Console")
    console.log(event);
    if (window.confirm('Are you sure you want to delete?')) {
      event.confirm.resolve();
    } else {
      event.confirm.reject();
    }
  }

  onCreateConfirm(event) {
    console.log("Create Event In Console")
    console.log(event);

  }

  onSaveConfirm(event) {
    console.log("Edit Event In Console")
    console.log(event);
  }

}