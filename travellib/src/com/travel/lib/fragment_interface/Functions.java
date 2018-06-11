package com.travel.lib.fragment_interface;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * 接口框架
 * 根据有无参数和返回值对接口方法进行了封装 （参照动脑学院的讲解视频）
 * Created by Administrator on 2017/5/16.
 */

public class Functions {
    HashMap<String, FunctionNoParamNoResult> mFunctionNoParamNoResultMap;
    HashMap<String, FunctionWithParamNoResult> mFunctionWithParamNoResultMap;
    HashMap<String, FunctionNoParamWithResult> mFunctionNoParamWithResultMap;
    HashMap<String, FunctionParamAndResult> mFunctionParamAndResultMap;

    public static class Function{
        String functionName;

        public Function(String functionName) {
            this.functionName = functionName;
        }
    }


    // （没有）参数（没有）返回值 的接口
    public static abstract class FunctionNoParamNoResult extends Function{

        public FunctionNoParamNoResult(String functionName) {
            super(functionName);
        }

        public abstract void function();
    }

    public Functions addFunction(FunctionNoParamNoResult function){
        if(function != null) {

            if (mFunctionNoParamNoResultMap == null) {
                mFunctionNoParamNoResultMap = new HashMap<>();
            }

            mFunctionNoParamNoResultMap.put(function.functionName, function);
        }
        return this;
    }

    public void invokeFunc(String funcName) throws NoFunctionException{
        if(TextUtils.isEmpty(funcName))
            return;

        if(mFunctionNoParamNoResultMap != null){
            FunctionNoParamNoResult func = mFunctionNoParamNoResultMap.get(funcName);
            if(func != null){
                func.function();
            } else {
                throw new NoFunctionException("has no function: " + funcName +
                        "found in FunctionNoParamNoResult");
            }
        }
    }



    // （有）参数（没有）返回值 的接口
    public static abstract class FunctionWithParamNoResult<Param> extends Function{

        public FunctionWithParamNoResult(String functionName) {
            super(functionName);
        }

        public abstract void function(Param param);
    }

    public Functions addFunction(FunctionWithParamNoResult function){
        if(function != null) {

            if (mFunctionWithParamNoResultMap == null) {
                mFunctionWithParamNoResultMap = new HashMap<>();
            }

            mFunctionWithParamNoResultMap.put(function.functionName, function);
        }
        return this;
    }

    public <Param> void invokeFunc(String funcName, Param param) throws NoFunctionException{
        if(TextUtils.isEmpty(funcName))
            return;

        if(mFunctionWithParamNoResultMap != null){
            FunctionWithParamNoResult func = mFunctionWithParamNoResultMap.get(funcName);
            if(func != null){
                func.function(param);
            } else {
                throw new NoFunctionException("has no function: " + funcName +
                        "found in FunctionWithParamNoResult");
            }
        }
    }



    // （没有）参数（有）返回值 的接口
    public static abstract class FunctionNoParamWithResult<Result> extends Function{

        public FunctionNoParamWithResult(String functionName) {
            super(functionName);
        }

        public abstract Result function();
    }

    public Functions addFunction(FunctionNoParamWithResult function){
        if(function == null)
            return this;

        if(mFunctionNoParamWithResultMap == null)
            mFunctionNoParamWithResultMap = new HashMap<>();

        mFunctionNoParamWithResultMap.put(function.functionName, function);

        return this;
    }

    public <Result> Result invokeFunc(String funcName, Class<Result> c) throws NoFunctionException{
        if (TextUtils.isEmpty(funcName))
            return null;

        if (mFunctionNoParamWithResultMap != null) {
            FunctionNoParamWithResult fc = mFunctionNoParamWithResultMap.get(funcName);
            if(fc != null){
                if(c != null)
                    return c.cast(fc.function()); // 绝对安全
                return (Result) fc.function();
            }else{
                throw new NoFunctionException("has no function: " + funcName +
                        "found in FunctionNoParamWithResult");
            }
        }

        return null;
    }



    // （有）参数（有）返回值 的接口
    public static abstract class FunctionParamAndResult<Result, Param> extends Function{

        public FunctionParamAndResult(String functionName) {
            super(functionName);
        }

        public abstract Result function(Param param);
    }

    public Functions addFunction(FunctionParamAndResult function){
        if(function == null)
            return this;

        if(mFunctionParamAndResultMap == null)
            mFunctionParamAndResultMap = new HashMap<>();

        mFunctionParamAndResultMap.put(function.functionName, function);

        return this;
    }

    /**
     *
     * @param funcName 方法名
     * @param c        返回类型
     * @param param    方法参数
     * @param <Result>
     * @param <Param>
     * @return
     * @throws NoFunctionException
     */
    public <Result, Param> Result invokeFunc(String funcName, Class<Result> c, Param param) throws NoFunctionException{
        if (TextUtils.isEmpty(funcName))
            return null;

        if (mFunctionParamAndResultMap != null) {
            FunctionParamAndResult fc = mFunctionParamAndResultMap.get(funcName);
            if(fc != null){
                if(c != null)
                    return c.cast(fc.function(param)); // 绝对安全
                return (Result) fc.function(param);
            }else{
                throw new NoFunctionException("has no function: " + funcName +
                        "found in FunctionParamAndResult");
            }
        }

        return null;
    }

}
