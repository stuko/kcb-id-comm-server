import React from 'react';
import logo from './logo.svg';
import './App.css';
import MainGrid from './components/main/MainGrid';
import RoutePath from './components/router/RoutePath';
import {BrowserRouter} from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
      <RoutePath/>
      </BrowserRouter>
    </div>
  );
}

export default App;
