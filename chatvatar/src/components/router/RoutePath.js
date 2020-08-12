import React from 'react';
import {Route , Switch} from 'react-router-dom';
import Home from "../home/Home";
import Login from "../user/Login";
import CVDraw from "../menu/CVDrawer";
export default () => (
    <div>
        <CVDrawer/>
        <Switch>
            <Route exact path="/" component={Home} />
            <Route exact path="/login" component={Login} />
        </Switch>

    </div>
)

