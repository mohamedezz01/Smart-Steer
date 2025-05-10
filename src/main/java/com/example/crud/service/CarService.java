package com.example.crud.service;

import com.example.crud.dao.CarRepository;
import com.example.crud.entity.Car;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car saveMessage(String message, String message2) {
        Car car = new Car();
        car.setMessage(message);
        car.setMessage2(message2);
        return carRepository.save(car);
    }


}
