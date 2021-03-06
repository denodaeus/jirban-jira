import {Component} from "@angular/core";
import {ROUTER_DIRECTIVES} from "@angular/router-deprecated";
import {BoardsService} from "../../services/boardsService";
import {TitleFormatService} from "../../services/TitleFormatService";
import {FormBuilder, ControlGroup, Validators} from "@angular/common";
import {RestUrlUtil} from "../../common/RestUrlUtil";
import {Http, Headers} from "@angular/http";
import {ProgressErrorService} from "../../services/progressErrorService";

/**
 * Backing class for functionality to explore the DB
 */
@Component({
    selector: 'boards',
    inputs: ['boards'],
    providers: [BoardsService],
    templateUrl: 'app/components/dbexplorer/dbexplorer.html',
    directives: [ROUTER_DIRECTIVES]
})
export class DbExplorerComponent {
    private sqlForm:ControlGroup;
    private error:string;
    private result:any;

    constructor(formBuilder:FormBuilder, title:TitleFormatService, private _http:Http) {
        title.setTitle("DB Explorer");
        this.sqlForm = formBuilder.group({
            "sql": ["", Validators.required]
        });
    }


    executeSql() {
        this.error = null;
        let url = 'rest/jirban/1.0/db-explorer';
        let path:string = RestUrlUtil.caclulateRestUrl(url);

        let headers:Headers = new Headers();
        headers.append("Content-Type", "application/json");
        headers.append("Accept", "application/json");
        let payload:any = {sql: this.sqlForm.value.sql};

        return this._http.post(path, JSON.stringify(payload),  {headers : headers})
            .timeout(60000, "The server did not respond in a timely manner for GET " + path)
            .subscribe(
                data => {
                    console.log(data);
                    this.result = JSON.parse(data["_body"]);
                },
                err => {
                    this.error = ProgressErrorService.parseErrorCodeAndString(err);
                    console.log(err);
                    this.result = null;
                }
            );
    }
}