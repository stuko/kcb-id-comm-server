import React, { Component } from "react";
import StackGrid from "react-stack-grid";
import { SizeMe } from 'react-sizeme'

export default class MainGrid extends Component {
    render() {
      return (
        <div>
        <StackGrid
          columnWidth={150}
        >
          <div key="key1">Item 1</div>
          <div key="key2">Item 2</div>
          <div key="key3">Item 3</div>
        </StackGrid>
        <SizeMe
            monitorHeight
            refreshRate={32}
            render={({ size }) => <div>My width is {size.width}px</div>}
        />
        </div>
      );
    }
  }