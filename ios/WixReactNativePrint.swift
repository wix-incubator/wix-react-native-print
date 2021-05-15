import Foundation
import UIKit

struct HtmlOptions {
    var htmlString: String
}

struct UrlOptions {
    var url: String
    var isLandscape: Bool
}

enum PrintErrorType: String {
    case BadArguments
    case UnsupportedFormat
    case PrintError
}

struct PrintError {
    var code: PrintErrorType
    var message: String
}


extension HtmlOptions: Codable {
    init(dictionary: NSDictionary) throws {
        self = try JSONDecoder().decode(HtmlOptions.self, from: JSONSerialization.data(withJSONObject: dictionary))
    }
}

extension UrlOptions: Codable {
    init(dictionary: NSDictionary) throws {
        self = try JSONDecoder().decode(UrlOptions.self, from: JSONSerialization.data(withJSONObject: dictionary))
    }
}

@objc(WixReactNativePrint)
class WixReactNativePrint: NSObject {

    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    private func cannotPrint() -> PrintError {
        return PrintError(code: PrintErrorType.UnsupportedFormat, message: "Cannot print this document")
    }
    
    private func createPrintInfo(orientation: UIPrintInfo.Orientation = .portrait) -> UIPrintInfo {
        let printInfo = UIPrintInfo(dictionary: nil)
        
        printInfo.orientation = orientation
        printInfo.outputType = .general
        printInfo.jobName = "Print Job"
        
        return printInfo
    }
    
    private func createPrintController(printInfo: UIPrintInfo? = nil) -> UIPrintInteractionController {
        let printController = UIPrintInteractionController.shared
                
        printController.printInfo = printInfo ?? createPrintInfo()
        
        return printController
    }
    
    private func doPrint(controller: UIPrintInteractionController, completion: @escaping (PrintError?) -> Void) -> Void {
        DispatchQueue.main.async {
            controller.present(animated: true) { (controller, success, error) in
                guard let printError = error as? UIPrintError, success else {
                    completion(nil)
                    return
                }
                completion(PrintError(code: PrintErrorType.PrintError, message: printError.localizedDescription))
            }
        }
    }
    
    private func printHtml(options: HtmlOptions, completion: @escaping (PrintError?) -> Void) {
        DispatchQueue.main.async {
            let printController = self.createPrintController()
        
            printController.printFormatter = UIMarkupTextPrintFormatter(markupText: options.htmlString)
            
            self.doPrint(controller: printController, completion: completion)
        }
    }
    
    private func printUrl(options: UrlOptions, completion: @escaping (PrintError?) -> Void) {
        print(options)

        guard let urlToPrint = URL(string: options.url), UIPrintInteractionController.canPrint(urlToPrint) else {
            completion(self.cannotPrint())
            return
        }
        
        URLSession.shared.dataTask(with: urlToPrint) { (data, urlResponse, error) in
            if let error = error {
               print("Request error: \(error)")
               completion(self.cannotPrint())
               return
             }
             
             guard let httpResponse = urlResponse as? HTTPURLResponse,
                   (200...299).contains(httpResponse.statusCode) else {
               print("Error response, unexpected status code: \(urlResponse!)")
               completion(self.cannotPrint())
               return
             }
            
            guard let data = data else { return }
            
            let printInfo = self.createPrintInfo(orientation: options.isLandscape ? .landscape : .portrait)
            let controller = self.createPrintController(printInfo: printInfo)
            
            controller.printingItems = [data]
            
            self.doPrint(controller: controller, completion: completion)
        }.resume()
    }

    //MARK: - Public
    @objc(printHtml:resolver:rejecter:)
    func printHtml(options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            let options = try HtmlOptions(dictionary: options)
            
            self.printHtml(options: options) {error in
                if (error != nil) {
                    reject(error?.code.rawValue, error?.message, nil)
                } else {
                    resolve(true)
                }
            }
        } catch {
            reject(PrintErrorType.BadArguments.rawValue, "Bad arguments provided for printHtml call", nil)
        }
    }
    
    @objc(printUrl:resolver:rejecter:)
    func printUrl(options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            let options = try UrlOptions(dictionary: options)
           
            self.printUrl(options: options) {error in
                if (error != nil) {
                    reject(error?.code.rawValue, error?.message, nil)
                } else {
                    resolve(true)
                }
            }
        } catch {
            reject(PrintErrorType.BadArguments.rawValue, "Bad arguments provided for printUrl call", nil)
        }
    }
}
