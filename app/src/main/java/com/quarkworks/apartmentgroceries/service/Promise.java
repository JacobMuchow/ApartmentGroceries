package com.quarkworks.apartmentgroceries.service;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class Promise {

    public interface Success {
        void onSuccess();
    }

    public interface Failure {
        void onFailure();
    }

    private Success success;
    private Failure failure;

    public void setSuccessCallack(Success success) {
        this.success = success;
    }

    public void setFailureCallback(Failure failure) {
        this.failure = failure;
    }

    public void setCallbacks(Success success, Failure failure) {
        this.success = success;
        this.failure = failure;
    }

    public void onSuccess() {
        if(success != null) {
            success.onSuccess();
        }
    }

    public void onFailure() {
        if(failure != null) {
            failure.onFailure();
        }
    }

    public void onSuccessOrFailure(Success success, Failure failure) {
        this.success = success;
        this.failure = failure;
    }
}
