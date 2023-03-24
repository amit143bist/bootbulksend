import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CacheDataLog } from './cachedatalog';

@Injectable({
  providedIn: 'root'
})
export class CacheDataLogService {

  private baseUrl = 'http://localhost:8290/docusign/cachelog';
  private httpOptions;

  constructor(private http: HttpClient) { 
    var headers_object = new HttpHeaders();
    headers_object = headers_object.append('Content-Type', 'application/json');
    headers_object = headers_object.append('Accept', 'application/json');
    headers_object = headers_object.append("Authorization", "Basic " + btoa("docusignuser:testing1"));

    this.httpOptions = {
      headers: headers_object
    };
  }

  getCacheDataLog(cacheId: string): Observable<any> {
    
    console.log('Inside getCacheDataLog with cacheId ' + cacheId);
    return this.http.get(`${this.baseUrl}/${cacheId}`, this.httpOptions);
  }

  createCacheDataLog(employee: Object): Observable<Object> {

    console.log('Inside createCacheDataLog with employee ' + JSON.stringify(employee));
    return this.http.post(`${this.baseUrl}`, employee, this.httpOptions);
  }

  updateCacheDataLog(cacheId: string, value: any): Observable<Object> {

    console.log('Inside updateCacheDataLog with cacheId ' + cacheId);
    return this.http.put(`${this.baseUrl}/${cacheId}`, value, this.httpOptions);
  }

  deleteCacheDataLogById(cacheId: string): Observable<any> {

    console.log('Inside deleteCacheDataLogById with cacheId ' + cacheId);
    return this.http.delete(`${this.baseUrl}/${cacheId}`, this.httpOptions);
  }

  getCacheDataLogList(): Observable<any> {

    console.log('Inside getCacheDataLogList with headers ' + this.httpOptions);
    return this.http.get(`${this.baseUrl}`, this.httpOptions);
  }
}