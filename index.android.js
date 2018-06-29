'use strict';
import { AppRegistry } from 'react-native';
import React, {Component} from 'react';
import {
    Platform,
    StyleSheet,
    Text,
    View, Image, TextInput, ScrollView, FlatList, SectionList
} from 'react-native';

const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
    android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});
console.log("dsdsdsd")
type Props = {};

//自定义组件
class Greening extends Component {
    render() {
        return (
            //自定义属性 {props} jsx的形式放在
            <Text>Hello {this.props.value}</Text>
        );
    }
}

class ShowText extends Component {
    constructor(propos) {
        super(propos)
        this.state = {showText: true}//设置自定义属性state默认值ture
        //设置定时器2秒每次变化一次state 值
        setInterval(() => {
            this.setState(previousState => {
                return {showText: !previousState.showText}
            });
            //set方法设置值
        }, 2000);
    }

    render() {
        let display = this.state.showText ? this.props.text : '';
        let style = this.props.style;
        return (
            <Text style={style}>{display}</Text>
        );
    }
}

//自定义TextView
class InputTextView extends Component {
    constructor(porps) {
        super(porps)
        this.state = {text: ''}
    }

    render() {
        return (<TextInput style={{color: 'red', fontWeight: 'bold', fontSize: 20}}
                           onChangeText={(text) => this.setState({text})}
                           onSubmitEditing={() => console.log(this.state.text)}
                           placeholder='请输入名字'
                           placeholderTextColor='gray'
                           value={this.state.text}
                           returnKeyType='done'
                           clearButtonMode='always'
                           underlineColorAndroid='transparent'
        />);
    }
}


export default class App extends Component<Props> {
    render() {
        return (
            <ScrollView style={{flex: 1}}>
                <View style={{flex: 1, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'}}>
                    <View style={{width: 40, height: 20, backgroundColor: 'red'}}>
                    </View>
                    <View style={{width: 40, height: 20, backgroundColor: 'steelblue'}}>
                    </View>
                </View>
                <TextInput style={{width: 200, height: 30, flex: 1}} onChangeText={() => {
                    console.log("dddd")
                }} onSubmitEditing={() => {
                    console.log("测试")
                }}/>
                <View style={{flex: 7}}>
                    {/*2个相同的text 放在一行，但是样式不一样*/}
                    <Text style={styles.baseText}>
                        <Text style={styles.bluecoclor} onPress={() => {
                            console.log("点击事件")
                        }}>dsadsds</Text>
                        <Text numberOfLines={4} style={styles.welcome}>
                            dasdadadaf22edd
                        </Text>
                    </Text>

                    <Text style={styles.bluecoclor}>
                        dsadsads xxxxxxxxxx
                    </Text>
                         <Image source={require('./assets/IconNew.png')} style={{width: 120, height: 80}}/>
                         <Image source={{uri: 'https://raw.githubusercontent.com/yangyunfeng666/image/master/room.png'}}
                           style={{width: 400, height: 400}}/>
                    <Greening value='dsdsddd'/>
                    <ShowText text='喂喂喂' style={styles.bluecoclor}/>
                    <InputTextView multiline={true} numberOfLines={4}
                    />
                </View>
                {/*列表数据*/}
                <FlatList style={{flex:4}}
                data={[{key: '暂时'},
                {key: '方法'},
                {key: '收到'},
                {key: '无损'},
                {key: '为何'}
                ]}
                renderItem={({item}) => <Text style={{color:'black', fontSize:22,height:20}}>{item.key}</Text>}
                />
                {/*列表数据*/}
                <SectionList
                    sections={[
                        {tiltle: 'J', data: ['dsdd']},
                        {tiltle: 'D', data: ['dsdd', 'ddd', 'dsda', 'dsadf3w']}
                    ]}
                    renderItem={({item}) => <Text style={styles.item}>{item} </Text>}
                    renderSelectionHeader={({selection}) => <Text style={styles.sectionHeader}>{selection.title}</Text>}>
                </SectionList>
            </ScrollView>
        );
    }


}


const styles = StyleSheet.create({
        sectionHeader:{
        paddingTop: 2,
        paddingLeft: 10,
        paddingRight: 10,
        paddingBottom: 2,
        fontSize: 14,
        fontWeight: 'bold',
        backgroundColor: 'rgba(247,247,247,1.0)',
    },
    item: {
        padding: 10,
        fontSize: 18,
        height: 44,
    },
    baseText: {
        fontFamily: 'Cochin',
    },
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
    bluecoclor: {
        color: 'red',
        fontWeight: 'bold',
        fontSize: 10
    }
});

AppRegistry.registerComponent('test', () => App);
